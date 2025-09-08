import { Priority } from "../../enum/priority.enum";
import { Status } from "../../enum/status.enum";

export interface BreakageFullDto {
    id: String;
    departmentName: String;
    room: String;
    breakageTopic: String;
    breakageText: String;
    status: Status;
    priority: Priority;
    executor: String;
    executorAppointedBy: String;
    createdBy: String;
    createdDate: Date;
    lastUpdatedBy: String;
    lastUpdatedDate: Date;
    deadline: Date;
    comments: Array<any>;
}