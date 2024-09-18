import { ActorDto } from "./actor.dto";


export class FilterAndSortEventsInfo {
    actors: string[] = [];
    sortType: string = "";
    isAscendantOrder: boolean= false;
    onlyNewMovies: boolean = false;

    constructor(data?: any) {
        Object.assign(this, data);
    }
}
