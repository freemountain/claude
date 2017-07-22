import * as through from "through2";

export const split = () => {
    let buffer = "";

    const stream = through((data: string, _, cb) => {
        const str = String(data);

        for (const char of str) {
            if (char !== "\n") {
                buffer += char;
                continue;
            }
            stream.push(buffer);
            buffer = "";
        }
        cb();
    });

    return stream;
};

export const parse = () => through.obj((data: string, _, cb) => {
    try {
        const obj = JSON.parse(data);
        cb(null, obj);
    } catch (e) {
        cb(e);
    }
});
