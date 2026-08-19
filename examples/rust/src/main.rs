//! Golden Path Rust hello stub.

fn main() {
    println!("hello FOSS");
}

#[cfg(test)]
mod tests {
    #[test]
    fn greets() {
        assert_eq!(greet(), "hello FOSS");
    }

    fn greet() -> &'static str {
        "hello FOSS"
    }
}
