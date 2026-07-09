create function set_updated_at()
  returns trigger
  language plpgsql
as
$$
begin
  new.updated_at = now();
  return new;
end;
$$;

create function is_stripped(input text)
  returns boolean
  language sql
  immutable
  parallel safe
as
$$
select input = btrim(
  input,
  E'\u0009\u000A\u000B\u000C\u000D\u001C\u001D\u001E\u001F \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2008\u2009\u200A\u2028\u2029\u205F\u3000'
);
$$;
