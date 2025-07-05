export class ChangeUserPasswordDto {
    newPassword: string;

    constructor(newPassword: string) {
        this.newPassword = newPassword;
    }
}