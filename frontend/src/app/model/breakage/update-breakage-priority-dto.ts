import { Priority } from "../../enum/priority.enum";
import { Status } from "../../enum/status.enum";

export class UpdateBreakagePriorityDto {
    priority: Priority;
    status: Status;

    constructor(priority: Priority, status: Status) {
        this.priority = priority;
        this.status = status;
    }
}