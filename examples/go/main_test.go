package main

import "testing"

func TestGreet(t *testing.T) {
	if Greet() != "hello FOSS" {
		t.Fatalf("unexpected greeting: %q", Greet())
	}
}
