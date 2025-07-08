import { ApiResponse } from "../model/response/api-response";

export class ApiResponseFactory {

    public static createEmptyApiResponse(): ApiResponse {
        return new ApiResponse('', 0, '', new Date);
    }
}