import { fromPairs } from "lodash";
import * as R from "ramda";

import parseRepository from "./parseRepository";

type Labels = R.Dictionary<string | number | boolean | null>;

const reduceLabels = (labels: Labels[]) => R.reduce(
    (all: Labels, curr: Labels) => Object.assign(all, curr),
    {} as Labels,
    labels,
);

const createLabels = (...labels: Labels[]): R.Dictionary<string> => {
    const reduced = reduceLabels(labels);
    const pairs = Object.keys(reduced)
        .map((label) => [label, reduced[label]])
        .map(([key, value]) => [`claude.${key}`, `${value}`]);

    return fromPairs(pairs);
};

const createLabelFilter = (...expectedLabels: Labels[]) => {
    const reduced = reduceLabels(expectedLabels);

    return (labels: R.Dictionary<string>) => R.pipe(
        R.keys,
        R.map((name: string) => {
            const expected = reduced[name];
            const labelName = `claude.${name}`;
            const actual = labels[labelName];

            return expected === null
                ? actual === undefined
                : actual === `${expected}`;
        }),
        R.all((isValid: boolean) => isValid),
    )(reduced);
};

export {
    createLabels,
    createLabelFilter,
    Labels,
    parseRepository,
};
