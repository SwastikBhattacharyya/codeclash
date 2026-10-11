const EXCERPT_LENGTH = 160;
const IMAGE = /!\[([^\]]*)\]\([^)]*\)/g;
const LINK = /\[([^\]]*)\]\([^)]*\)/g;
const HEADING = /^\s{0,3}#{1,6}\s/;
const INLINE_MARKS = /(\*\*|__|[*`~>$|])/g;

export function excerpt(markdown: string): string {
  const paragraph =
    markdown
      .split(/\n\s*\n/)
      .filter((block) => !HEADING.test(block))
      .map((block) =>
        block
          .replace(IMAGE, '$1')
          .replace(LINK, '$1')
          .replace(INLINE_MARKS, '')
          .replace(/\s+/g, ' ')
          .trim(),
      )
      .find((block) => block !== '') ?? '';
  const characters = [...paragraph];
  return characters.length > EXCERPT_LENGTH
    ? `${characters
        .slice(0, EXCERPT_LENGTH - 1)
        .join('')
        .trimEnd()}…`
    : paragraph;
}
