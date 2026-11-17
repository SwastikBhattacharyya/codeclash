package config

import (
	"errors"
	"fmt"
	"regexp"
	"strconv"
	"strings"
	"unicode"
	"unicode/utf8"

	"golang.org/x/text/unicode/norm"
)

const (
	MinUnprivilegedPort  = 1024
	MaxPort              = 65535
	MaxCount             = 999
	MaxProxyHops         = 5
	MaxDomainLength      = 200
	MaxLabelLength       = 63
	MinOpsPasswordSize   = 12
	KibibytesPerMebibyte = 1024
	AdminUsernameMinSize = 3
	AdminUsernameMaxSize = 32
	AdminNameMinSize     = 2
	AdminNameMaxSize     = 255
	AdminEmailMaxSize    = 254
	AdminPasswordMinSize = 8
	AdminPasswordMaxSize = 256
)

var (
	reName          = regexp.MustCompile(`^[a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?$`)
	reK8sSize       = regexp.MustCompile(`^[0-9]+(Mi|Gi)$`)
	reSpringSize    = regexp.MustCompile(`^[0-9]+(MB|GB)$`)
	reURL           = regexp.MustCompile(`^https?://[^/\s]+(/\S*)?$`)
	reJDBC          = regexp.MustCompile(`^jdbc:postgresql://[^@\s]+/[^/\s?]+(\?\S*)?$`)
	reHost          = regexp.MustCompile(`^[A-Za-z0-9.-]+$`)
	reEmail         = regexp.MustCompile(`^[^@\s]+@[^@\s]+$`)
	reLooseName     = regexp.MustCompile(`^[A-Za-z0-9_.@+-]+$`)
	reProbability   = regexp.MustCompile(`^(0(\.[0-9]+)?|1(\.0+)?)$`)
	reHostList      = regexp.MustCompile(`^(\*\.)?[A-Za-z0-9][A-Za-z0-9.-]*(,(\*\.)?[A-Za-z0-9][A-Za-z0-9.-]*)*$`)
	reRegistry      = regexp.MustCompile(`^[A-Za-z0-9.-]+(:[0-9]+)?(/[a-z0-9._-]+)*$`)
	reAdminUsername = regexp.MustCompile(`^[A-Za-z0-9_-]+$`)
	reAdminName     = regexp.MustCompile(`[a-zA-Z].*[a-zA-Z]`)
	reProfileName   = regexp.MustCompile(`^[a-z0-9][a-z0-9-]{0,31}$`)
	reContext       = regexp.MustCompile(`^\S+$`)
	reDomain        = regexp.MustCompile(`^([a-z0-9]([a-z0-9-]*[a-z0-9])?\.)*[a-z0-9]([a-z0-9-]*[a-z0-9])?$`)
)

func plain(value string) error {
	if value == "" {
		return errors.New("a value is required")
	}
	if strings.ContainsAny(value, " \t\n\"'\\") {
		return errors.New("spaces, quotes and backslashes are not allowed")
	}
	return nil
}

func matching(re *regexp.Regexp, what string) func(string) error {
	return func(value string) error {
		if err := plain(value); err != nil {
			return err
		}
		if !re.MatchString(value) {
			return fmt.Errorf("%q is not a valid %s", value, what)
		}
		return nil
	}
}

var (
	ValidateName        = matching(reName, "name (lowercase letters, digits and dashes)")
	ValidateK8sSize     = matching(reK8sSize, "Kubernetes size such as 768Mi or 2Gi")
	ValidateSpringSize  = matching(reSpringSize, "size such as 512MB")
	ValidateURL         = matching(reURL, "http(s) URL")
	ValidateHost        = matching(reHost, "hostname")
	ValidateEmail       = matching(reEmail, "email address")
	ValidateLooseName   = matching(reLooseName, "username")
	ValidateProbability = matching(reProbability, "probability between 0 and 1")
	ValidateHostList    = matching(reHostList, "comma-separated hostname list")
	ValidateProfileName = matching(reProfileName, "profile name (lowercase letters, digits and dashes)")
	ValidateContext     = matching(reContext, "kubeconfig context")
	reNumericDomain     = regexp.MustCompile(`^[0-9.]+$`)
)

func ValidateJDBC(value string) error {
	if err := matching(reJDBC, "JDBC URL such as jdbc:postgresql://host:5432/db")(value); err != nil {
		return err
	}
	if strings.Contains(strings.ToLower(value), "password=") {
		return errors.New("keep the password out of the URL")
	}
	return nil
}

func Mebibytes(size string) (int64, error) {
	units := map[string]int64{"Ki": 0, "Mi": 1, "Gi": 1 << 10, "Ti": 1 << 20}
	for suffix, factor := range units {
		number, found := strings.CutSuffix(size, suffix)
		if !found {
			continue
		}
		value, err := strconv.ParseInt(number, 10, 64)
		if err != nil {
			break
		}
		if factor == 0 {
			return value / KibibytesPerMebibyte, nil
		}
		return value * factor, nil
	}
	return 0, fmt.Errorf("%q is not a size in Ki, Mi, Gi or Ti", size)
}

func ValidateRegistry(value string) error {
	if value == "" {
		return nil
	}
	return matching(reRegistry, "registry such as registry.example.com:5000/team")(value)
}

func ValidatePort(value string) error {
	return portIn(value, 1)
}

func ValidateHostPort(value string) error {
	return portIn(value, MinUnprivilegedPort)
}

func portIn(value string, low int) error {
	port, err := strconv.Atoi(value)
	if err != nil || port < low || port > MaxPort {
		return fmt.Errorf("a port between %d and %d is required", low, MaxPort)
	}
	return nil
}

func ValidateCount(value string) error {
	return countIn(value, 0)
}

func ValidatePositiveCount(value string) error {
	return countIn(value, 1)
}

func countIn(value string, low int) error {
	count, err := strconv.Atoi(value)
	if err != nil || count < low || count > MaxCount {
		return fmt.Errorf("a number between %d and %d is required", low, MaxCount)
	}
	return nil
}

func ValidateSecret(value string) error {
	if value == "" {
		return errors.New("a value is required")
	}
	if strings.ContainsAny(value, "\n\r") {
		return errors.New("line breaks are not allowed")
	}
	return nil
}

func ValidateDomain(value string) error {
	if err := matching(reDomain, "lowercase domain such as codeclash.example.com")(value); err != nil {
		return err
	}
	if reNumericDomain.MatchString(value) {
		return errors.New("use a domain name, not an IP address")
	}
	if len(value) > MaxDomainLength {
		return fmt.Errorf("at most %d characters", MaxDomainLength)
	}
	for _, label := range strings.Split(value, ".") {
		if len(label) > MaxLabelLength {
			return fmt.Errorf("each label is at most %d characters", MaxLabelLength)
		}
	}
	return nil
}

func ValidateOpsEmail(value string) error {
	if err := ValidateEmail(value); err != nil {
		return err
	}
	if strings.ContainsFunc(value, func(r rune) bool { return r == ':' || unicode.IsSpace(r) }) {
		return errors.New("colons and spaces are not allowed")
	}
	return nil
}

func ValidateProxyHops(value string) error {
	hops, err := strconv.Atoi(value)
	if err != nil || hops < 0 || hops > MaxProxyHops {
		return fmt.Errorf("a number between 0 and %d is required", MaxProxyHops)
	}
	return nil
}

func ValidateOpsPassword(value string) error {
	if utf8.RuneCountInString(value) < MinOpsPasswordSize {
		return fmt.Errorf("at least %d characters", MinOpsPasswordSize)
	}
	if strings.HasPrefix(value, "-") {
		return errors.New("must not start with -")
	}
	if strings.ContainsAny(value, ": \t\n\r\"'\\") {
		return errors.New("colons, spaces, quotes and backslashes are not allowed")
	}
	return nil
}

func ValidateAdminUsername(value string) error {
	size := utf8.RuneCountInString(value)
	if size < AdminUsernameMinSize || size > AdminUsernameMaxSize {
		return fmt.Errorf("between %d and %d characters", AdminUsernameMinSize, AdminUsernameMaxSize)
	}
	if !reAdminUsername.MatchString(value) {
		return errors.New("only letters, digits, _ and -")
	}
	return nil
}

func ValidateAdminName(value string) error {
	size := utf8.RuneCountInString(value)
	if size < AdminNameMinSize || size > AdminNameMaxSize {
		return fmt.Errorf("between %d and %d characters", AdminNameMinSize, AdminNameMaxSize)
	}
	if strings.TrimSpace(value) != value || strings.ContainsAny(value, "\n\r\u0085  ") {
		return errors.New("no surrounding spaces or line breaks")
	}
	if !reAdminName.MatchString(value) {
		return errors.New("needs at least two ASCII letters")
	}
	return nil
}

func ValidateAdminEmail(value string) error {
	if utf8.RuneCountInString(value) > AdminEmailMaxSize {
		return fmt.Errorf("at most %d characters", AdminEmailMaxSize)
	}
	return ValidateEmail(value)
}

func ValidateAdminPassword(value string) error {
	normalized := norm.NFKC.String(value)
	size := utf8.RuneCountInString(normalized)
	if size < AdminPasswordMinSize || size > AdminPasswordMaxSize {
		return fmt.Errorf("between %d and %d characters", AdminPasswordMinSize, AdminPasswordMaxSize)
	}
	var lower, upper, digit, special bool
	for _, r := range normalized {
		switch {
		case unicode.IsLower(r):
			lower = true
		case unicode.IsUpper(r):
			upper = true
		case unicode.IsDigit(r):
			digit = true
		case !unicode.IsLetter(r) && !unicode.IsSpace(r) && !unicode.IsControl(r) && !unicode.In(r, unicode.Cf):
			special = true
		}
	}
	if !lower || !upper || !digit || !special {
		return errors.New("needs a lowercase letter, an uppercase letter, a digit and a special character")
	}
	return nil
}

func (p *Profile) Validate() error {
	var problems []string
	check := func(field string, err error) {
		if err != nil {
			problems = append(problems, field+": "+err.Error())
		}
	}
	check("admin.username", ValidateAdminUsername(p.Admin.Username))
	check("admin.name", ValidateAdminName(p.Admin.Name))
	check("admin.email", ValidateAdminEmail(p.Admin.Email))
	check("admin.password", ValidateAdminPassword(p.Admin.Password))
	if p.Contestants.Password != "" {
		check("contestants.password", ValidateAdminPassword(p.Contestants.Password))
	}
	switch p.Target {
	case TargetCompose:
	case TargetK8s:
		if p.K8s == nil {
			problems = append(problems, "k8s: section missing")
		} else {
			problems = append(problems, p.K8s.problems()...)
		}
	default:
		problems = append(problems, fmt.Sprintf("target: %q is neither %s nor %s", p.Target, TargetCompose, TargetK8s))
	}
	if len(problems) > 0 {
		return fmt.Errorf("profile %s is incomplete, run configure:\n  %s", p.Name, strings.Join(problems, "\n  "))
	}
	return nil
}

func (k *K8s) problems() []string {
	var problems []string
	check := func(field string, err error) {
		if err != nil {
			problems = append(problems, "k8s."+field+": "+err.Error())
		}
	}
	switch k.Cluster {
	case ClusterKind:
		check("kind_name", ValidateName(k.KindName))
		check("public_port", ValidateHostPort(strconv.Itoa(k.PublicPort)))
	case ClusterExisting:
		check("context", ValidateContext(k.Context))
	default:
		problems = append(problems, fmt.Sprintf("k8s.cluster: %q is neither %s nor %s", k.Cluster, ClusterKind, ClusterExisting))
	}
	check("namespace", ValidateName(k.Namespace))
	check("registry", ValidateRegistry(k.Registry))
	check("web_allowed_hosts", ValidateHostList(k.WebAllowedHosts))
	check("otlp_endpoint", ValidateURL(k.OTLPEndpoint))
	check("tracing_sampling", ValidateProbability(k.TracingSampling))
	check("service_memory_limit", ValidateK8sSize(k.ServiceMemoryLimit))
	check("web_memory_limit", ValidateK8sSize(k.WebMemoryLimit))
	check("dind_memory_limit", ValidateK8sSize(k.DindMemoryLimit))
	check("runner_warm_size", ValidateCount(strconv.Itoa(k.RunnerWarmSize)))
	check("sandbox_memory", ValidateSpringSize(k.SandboxMemory))
	check("runner_max_concurrent", ValidatePositiveCount(strconv.Itoa(k.RunnerMaxConcurrent)))
	check("postgres_storage", ValidateK8sSize(k.PostgresStorage))
	check("domain", ValidateDomain(k.Domain))
	check("trusted_proxy_hops", ValidateProxyHops(strconv.Itoa(k.TrustedProxyHops)))
	check("ops.email", ValidateOpsEmail(k.Ops.Email))
	check("ops.password", ValidateOpsPassword(k.Ops.Password))
	if !k.IsKind() {
		check("http_port", ValidatePort(strconv.Itoa(k.HTTPPort)))
		check("https_port", ValidatePort(strconv.Itoa(k.HTTPSPort)))
		if k.HTTPPort == k.HTTPSPort {
			problems = append(problems, "k8s.https_port: must differ from http_port")
		}
	}
	if k.TLS.Enabled {
		if k.HTTPPort != DefaultHTTPPort {
			problems = append(problems, "k8s.http_port: Let's Encrypt HTTP-01 needs 80 when TLS is on")
		}
		if k.TLS.Issuer != IssuerStaging && k.TLS.Issuer != IssuerProduction {
			problems = append(problems, "k8s.tls.issuer: must be staging or production")
		}
		if k.IsKind() || !strings.Contains(k.Domain, ".") {
			problems = append(problems, "k8s.tls.enabled: needs an existing cluster and a public domain")
		}
		check("tls.email", ValidateEmail(k.TLS.Email))
	}
	switch k.Mail.Mode {
	case MailMailpit:
	case MailExternal:
		check("mail.host", ValidateHost(k.Mail.Host))
		check("mail.port", ValidatePort(strconv.Itoa(k.Mail.Port)))
		if k.Mail.Security != SecurityStartTLS && k.Mail.Security != SecurityTLS && k.Mail.Security != SecurityNone {
			problems = append(problems, "k8s.mail.security: must be starttls, tls or none")
		}
		if k.Mail.User != "" {
			check("mail.user", ValidateLooseName(k.Mail.User))
			check("mail.password", ValidateSecret(k.Mail.Password))
		}
	default:
		problems = append(problems, "k8s.mail.mode: must be mailpit or external")
	}
	check("mail.from", ValidateEmail(k.Mail.From))
	for _, service := range DBServices {
		database := k.Databases[service]
		field := "databases." + service
		if database == nil {
			problems = append(problems, "k8s."+field+": missing")
			continue
		}
		if database.Mode != ModeLocal && database.Mode != ModeExternal {
			problems = append(problems, "k8s."+field+".mode: must be local or external")
		}
		check(field+".url", ValidateJDBC(database.URL))
		check(field+".user", ValidateLooseName(database.User))
		check(field+".password", ValidateSecret(database.Password))
	}
	return problems
}
