export class CreateBreakageDto {
    room: string;
    breakageTopic: string;
    breakageText: string;

    constructor(room: string, breakageTopic: string, breakageText: string) {
        this.room = room;
        this.breakageTopic = breakageTopic;
        this.breakageText = breakageText;
    }
}