import { Status } from "../../enum/status.enum";

export class AppointBreakageExecutorDto {
    executor: string;
    deadline: Date;
    status: Status;

    constructor(executor: string, deadline: Date, status: Status) {
        this.executor = executor;
        this.deadline = deadline;
        this.status = status;
    }
}