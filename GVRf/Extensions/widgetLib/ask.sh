# This is a general-purpose function to ask Yes/No questions in Bash, either
# with or without a default answer. It keeps repeating the question until it
# gets a valid answer.

ask() {
    # http://djm.me/ask
    while true; do

        if [ "${2:-}" = "Y" ]; then
            prompt="Y/n"
            default=Y
        elif [ "${2:-}" = "N" ]; then
            prompt="y/N"
            default=N
        else
            prompt="y/n"
            default=
        fi

        # Ask the question (not using "read -p" as it uses stderr not stdout)
        echo -n "$1 [$prompt] "

        # Read the answer (use /dev/tty in case stdin is redirected from somewhere else)
        read REPLY </dev/tty

        # Default?
        if [ -z "$REPLY" ]; then
            REPLY=$default
        fi

        # Check if the reply is valid
        case "$REPLY" in
            Y*|y*) return 0 ;;
            N*|n*) return 1 ;;
        esac

    done
}

export -f ask

example() {
    # EXAMPLE USAGE:

    if ask "Do you want to do such-and-such?"; then
        echo "Yes"
    else
        echo "No"
    fi

    # Default to Yes if the user presses enter without giving an answer:
    if ask "Do you want to do such-and-such?" Y; then
        echo "Yes"
    else
        echo "No"
    fi

    # Default to No if the user presses enter without giving an answer:
    if ask "Do you want to do such-and-such?" N; then
        echo "Yes"
    else
        echo "No"
    fi

    # Only do something if you say Yes
    if ask "Do you want to do such-and-such?"; then
        said_yes
    fi

    # Only do something if you say No
    if ! ask "Do you want to do such-and-such?"; then
        said_no
    fi

    # Or if you prefer the shorter version:
    ask "Do you want to do such-and-such?" && said_yes

    ask "Do you want to do such-and-such?" || said_no
    # EXAMPLE USAGE:

    if ask "Do you want to do such-and-such?"; then
        echo "Yes"
    else
        echo "No"
    fi

    # Default to Yes if the user presses enter without giving an answer:
    if ask "Do you want to do such-and-such?" Y; then
        echo "Yes"
    else
        echo "No"
    fi

    # Default to No if the user presses enter without giving an answer:
    if ask "Do you want to do such-and-such?" N; then
        echo "Yes"
    else
        echo "No"
    fi

    # Only do something if you say Yes
    if ask "Do you want to do such-and-such?"; then
        said_yes
    fi

    # Only do something if you say No
    if ! ask "Do you want to do such-and-such?"; then
        said_no
    fi

    # Or if you prefer the shorter version:
    ask "Do you want to do such-and-such?" && said_yes

    ask "Do you want to do such-and-such?" || said_no
}

