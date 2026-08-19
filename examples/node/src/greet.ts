export function greet(name: string): string {
  const trimmed = name.trim();
  const target = trimmed.length > 0 ? trimmed : "world";
  return `Hello, ${target}!`;
}
