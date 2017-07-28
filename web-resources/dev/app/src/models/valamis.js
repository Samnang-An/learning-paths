import Backbone from "backbone";
import appConfig from "config";

const ValamisModel = Backbone.Model.extend({
    urlRoot: function () {
        return `${appConfig().apiPath}${this.options.url(this)}`;
    }
});

export default ValamisModel