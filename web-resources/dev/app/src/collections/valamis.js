import _ from "lodash";
import Backbone from "backbone";
import appConfig from "config";


export default class ValamisCollection extends Backbone.Collection {
    //todo override or add here some methods or something
    initialize(options) {
        this.url = `${appConfig().apiPath}${_.result(this, 'url')}`;
    }
}
