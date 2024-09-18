import { ActorDto } from "./actor.dto";

export class MovieDto {
    name : string = '';
    year: number = 0;
    imgSrc : string = "";
    description : string = "";
    genre : string = "";
    rdfTurtle : string = "";
    url : string = "";
    actors : ActorDto[] = [];

    constructor(data?: any) {
        Object.assign(this, data);
    }
}
