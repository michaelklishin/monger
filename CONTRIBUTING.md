## Pre-requisites

The project uses [Leiningen 2](http://leiningen.org) and requires a recent MongoDB to be running
locally. Make sure you have those two installed and then run tests against all supported Clojure versions using

    ./bin/ci/before_script.sh
    lein all do clean, javac, test


## Pull Requests

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, write a [good, detailed commit message](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) submit a pull request on GitHub.
