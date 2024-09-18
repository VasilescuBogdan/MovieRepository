export class UserDto {
    firstName : string = '';
    secondName: string = '';
    university: string = '';
    email: string = '';
    role: string = '';
    dateOfBirth : Date = new Date();

    constructor(data?: any) {
        Object.assign(this, data);
    }
}
