# Import babshka base Docker image
FROM borkdude/babashka

# Copies babashka script from action repository to the filesystem path `/` of the container
COPY bb.clj /bb.sh

# Code file to execute when the docker container starts up (`bb.sh`)
ENTRYPOINT ["/bb.sh"]
