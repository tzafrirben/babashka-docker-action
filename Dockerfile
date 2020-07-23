# Base container image
FROM borkdude/babashka

# Copy entry point script from action repository to the filesystem path `/` of the container
COPY entrypoint.bb.clj /entrypoint.bb.clj

# Code file to execute when the Docker container starts up (`entrypoint.bb.clj`)
ENTRYPOINT ["bb", "/entrypoint.bb.clj"]
