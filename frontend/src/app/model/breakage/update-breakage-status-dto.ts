import { Status } from "../../enum/status.enum";

export class UpdateBreakageStatusDto {
    status: Status;

    constructor(status: Status) {
        this.status = status;
    }
}