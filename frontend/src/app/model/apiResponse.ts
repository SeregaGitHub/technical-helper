export class ApiResponse {
    message: string;
    status: number;
    httpStatus: string;
    timestamp: Date

    constructor(message: string, status: number, httpStatus: string, timestamp: Date) {
        this.message = message;
        this.status = status;
        this.httpStatus = httpStatus;
        this.timestamp = timestamp;
    }
}