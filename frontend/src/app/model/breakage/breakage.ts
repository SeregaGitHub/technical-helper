import { Priority } from "../../enum/priority.enum";
import { Status } from "../../enum/status.enum";


export interface Breakage {
    departmentName: String;
    room: String;
    breakageTopic: String;
    status: Status;
    priority: Priority;
    executor: String;
    createdBy: String;
    createdDate: Date;
}