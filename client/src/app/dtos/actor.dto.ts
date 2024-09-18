export class ActorDto {
    name : string = '';

    constructor(data?: any) {
        Object.assign(this, data);
    }
}
