// Golden Path Go hello + crash sanitize (no network, no PII).
package main

import "fmt"

func main() {
	fmt.Println(Greet())
}
