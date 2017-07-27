import ValamisModel from 'models/valamis';
import appConfig from "config";

const CourseService = new Backbone.Service({
    url: function () {
        return appConfig().endpoint.courses;
    }
});

const Course = ValamisModel.extend(CourseService);

export default Course