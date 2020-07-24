## babashka

Run [babashka](https://github.com/borkdude/babashka) script\shell commands.

## Action inputs
You can choose to either execute a babashka script or babashka shell command(s) (`bb`)

`bb_src` path to babashka script to execute  
`bb_args` arguments to pass to the babashka script  

Or if you choose to execute babashka shell commands  
`bb_cmd` the commands to execute

## Action outputs
The script\shell actions sets output into `bb_out` var.

## Example usage
To use the action create an babashka.yml (or choose custom *.yml name) in the .github/workflows/ directory.

To execute a babashka script
```yaml
- name: Execute babashak script
  uses: tzafrirben/babashka-docker-action@v0.3
  with:
    bb_src: <path-to-babashka-script>
    bb_args: <bb-srcipt-arguments> (optional)
```
To execute babashka shell commands (`bb`)
```yaml
- name: Excecute babashka shell command(s)
  uses: tzafrirben/babashka-docker-action@v0.3
  with:
    bb_cmd: <command(s)>
```
See [action.yml](action.yml) for the full documentation for this action's inputs and outputs.

### babashka script example
In order to execute a babashka script from your git repository, you must first checkout the git repository in the virtual environment.  

```yaml
on: [push]
jobs:
  babashka_job:
    runs-on: ubuntu-latest
    name: Execute babashka script
    steps:
      # To use a script from the repository,
      # you must check out the repository first
      - name: Checkout
        uses: actions/checkout@v2
      # Now we can execute a babashak script from our
      # repository
      - name: babashka script
        uses: tzafrirben/babashka-docker-action@v0.3
        id: bb_script
        with:
          bb_src: '<path-to-script-in-repo>'
          bb_args: '1 2 3 ...'
      # Print the output of the babashka script from the
      # `bb_script` step 
      - name: Get the script output
        run: echo "${{ steps.bb_script.outputs.bb_out }}"
```

### babashka shell example
In order to execute a babashka shell command use the `bb` piped with other shell command(s)

```yaml
on: [push]
jobs:
  babashka_job:
    runs-on: ubuntu-latest
    name: Execute babashka shell commands
    steps:
      - name: babashka shell
        uses: tzafrirben/babashka-docker-action@v0.3
        id: bb_shell
        with:
          bb_cmd: "ls | bb -i '(take 2 *input*)'"
      # Print the output of the babashka shell command from the
      # `bb_shell` step 
      - name: Get the script output
        run: echo "${{ steps.bb_shell.outputs.bb_out }}"
``
