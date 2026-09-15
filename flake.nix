{
  description = "Optional wrappers for existing agent-project-bootstrap scripts (not a second generator CLI). .#verify runs scripts/verify.sh only — missing toolchains/pruned stacks are skipped by that script, not by Nix.";

  outputs =
    { self }:
    let
      apps = {
        verify = {
          type = "app";
          program = "${self}/scripts/verify.sh";
        };
        validate-bootstrap = {
          type = "app";
          program = "${self}/scripts/validate-bootstrap.sh";
        };
        feature-gate = {
          type = "app";
          program = "${self}/scripts/feature-gate.sh";
        };
        update-deps = {
          type = "app";
          program = "${self}/scripts/update-deps.sh";
        };
      };
    in
    {
      apps.x86_64-linux = apps;
      apps.aarch64-linux = apps;
      apps.x86_64-darwin = apps;
      apps.aarch64-darwin = apps;
    };
}
