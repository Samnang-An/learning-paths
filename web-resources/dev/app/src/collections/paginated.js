import _ from 'lodash';
import { mixin } from 'core-decorators';
import ValamisCollection from 'collections/valamis';

@mixin ({
    defaults: {
        page: 0,
        itemsPerPage: 2,
        total: 0,
        items: []
    }
})
export default class PaginatedCollection extends ValamisCollection {
    initialize(options){
        this.options = _.defaults(options || {}, this.defaults);
        return super.initialize(options);
    }
    parse(response) {
        //const pagination= response.meta.pagination;
        this.options.total = response.total;
        this.options.page = response.page;

        return super.parse(response.items);
    }

    getPage(){
        return this.options.page;
    }
    fetchByPage(page = 0) {
        let itemsPerPage = this.options.itemsPerPage;

        return this.fetch({ data: {
                'page': page,
                'itemsPerPage': itemsPerPage
            }
        });
    }

    nextPage(){
        //TODO add page number checking
        this.options.page = this.options.page + 1;

        this.fetchByPage(this.options.page);
    }

    previousPage(){
        //TODO add page number checking
        this.options.page = this.options.page - 1;

        this.fetchByPage(this.options.page);
    }

    hasItems() {
        return this.options.total > 0;
    }

    hasMore() {
        return this.length < this.options.total
    }
}
