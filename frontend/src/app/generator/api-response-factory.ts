import { ApiResponse } from "../model/api-response";

export class ApiResponseFactory {

    public static createEmptyApiResponse(): ApiResponse {
        return new ApiResponse('', 0, '', new Date);
    }
}