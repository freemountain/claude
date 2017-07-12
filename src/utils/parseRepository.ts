const parseRepository = (input: string) => {
    let separatorPos;
    const digestPos = input.indexOf("@");
    const colonPos = input.lastIndexOf(":");
    // @ symbol is more important
    if (digestPos >= 0) {
        separatorPos = digestPos;
    } else if (colonPos >= 0) {
        separatorPos = colonPos;
    } else {
        return { repository: input, tag: "latest" };
    }

    // last colon is either the tag (or part of a port designation)
    const tag = input.slice(separatorPos + 1);

    // if it contains a / its not a tag and is part of the url
    if (tag.indexOf("/") === -1) {
        return {
            repository: input.slice(0, separatorPos),
            tag,
        };
    }

    return { repository: input, tag: "latest" };
};

export default parseRepository;