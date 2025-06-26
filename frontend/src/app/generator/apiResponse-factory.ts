import { ApiResponse } from "../model/apiResponse";

export class ApiResponseFactory {

    public static createEmptyApiResponse(): ApiResponse {
        return new ApiResponse('', 0, '', new Date);
    }
}