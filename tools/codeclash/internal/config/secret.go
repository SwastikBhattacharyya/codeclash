package config

import (
	"crypto/rand"
	"math/big"
	"strings"
)

const (
	SecretLength   = 32
	PasswordLength = 24
	lowercase      = "abcdefghijklmnopqrstuvwxyz"
	uppercase      = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	digits         = "0123456789"
	specials       = "-_.!@#%^*+="
	alphanumeric   = lowercase + uppercase + digits
)

func GenerateSecret(length int) (string, error) {
	return pick(alphanumeric, length)
}

func GeneratePassword() (string, error) {
	var builder strings.Builder
	for _, set := range []string{lowercase, uppercase, digits, specials} {
		chunk, err := pick(set, 1)
		if err != nil {
			return "", err
		}
		builder.WriteString(chunk)
	}
	rest, err := pick(alphanumeric+specials, PasswordLength-builder.Len())
	if err != nil {
		return "", err
	}
	builder.WriteString(rest)
	return shuffle(builder.String())
}

func pick(set string, length int) (string, error) {
	out := make([]byte, length)
	limit := big.NewInt(int64(len(set)))
	for i := range out {
		n, err := rand.Int(rand.Reader, limit)
		if err != nil {
			return "", err
		}
		out[i] = set[n.Int64()]
	}
	return string(out), nil
}

func shuffle(s string) (string, error) {
	out := []byte(s)
	for i := len(out) - 1; i > 0; i-- {
		n, err := rand.Int(rand.Reader, big.NewInt(int64(i+1)))
		if err != nil {
			return "", err
		}
		j := n.Int64()
		out[i], out[j] = out[j], out[i]
	}
	return string(out), nil
}
