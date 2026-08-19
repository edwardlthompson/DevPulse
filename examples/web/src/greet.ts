export function greet(name: string): string {
  if (!name.trim()) {
    return "Hello, world!";
  }
  return `Hello, ${name.trim()}!`;
}

export function isOnline(): boolean {
  return typeof navigator !== "undefined" ? navigator.onLine : true;
}
