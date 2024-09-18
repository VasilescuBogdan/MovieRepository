export class Actor {
    name : string = '';

    constructor(data?: any) {
        Object.assign(this, data);
    }
}