interface IApplicationLogEvent {
    level: number;
    levelName: string;
    message: string;
    meta?: object;
    source: string;
}

export default IApplicationLogEvent;
