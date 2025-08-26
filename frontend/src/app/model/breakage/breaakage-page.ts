import { BreakageDto } from "./breakage-dto";

export interface BreakagePage {
    content: BreakageDto[],
    totalElements: number,
	totalPages: number,
	numberOfElements: number,
	pageNumber: number,
	pageSize: number,
	offset: number,
	first: boolean,
	last: boolean
}