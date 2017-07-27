import ValamisModel from 'models/valamis';
import appConfig from "config";
import LiferayUtil from 'utils/liferay-utils';

const LessonModelService = new Backbone.Service({
    targets: {
        'getTincanLessonInfo': {
            'path': (model) => {
                return appConfig().endpoint.sequencing + model.get('id');
            },
            'data': () => {
                return { 'p_auth': LiferayUtil.authToken() }
            },
            'method': 'post'
        },
        'getLrsSettings': {
            'path': () => {
                return appConfig().endpoint.lrsSettings
            },
            'method': 'get'
        },
        'getLessonInfo': {
            'path': (model) => {
                return appConfig().endpoint.lessonsPublic + model.get('id')
            },
            'data': () => {
                return { 'courseId': LiferayUtil.courseId() }
            },
            'method': 'get'
        }
    }
});

const LessonModel = ValamisModel.extend({
    initialize() {
        this.url = appConfig().apiPath;
    },
    getLessonUrl() {
        return LiferayUtil.pathMain() + '/portal/learn-portlet/open_package'
            + '?plid=' + LiferayUtil.plId() + '&oid=' + this.get('id');
    }
}).extend(LessonModelService);

export default LessonModel