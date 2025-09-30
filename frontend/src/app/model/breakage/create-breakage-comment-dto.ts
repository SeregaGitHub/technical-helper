import { Status } from "../../enum/status.enum";

export class CreateBreakageCommentDto {
    comment: string;
    status: Status;

    constructor(comment: string, status: Status) {
        this.comment = comment;
        this.status = status;
    }
}