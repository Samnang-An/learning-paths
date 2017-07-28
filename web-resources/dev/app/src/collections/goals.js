import ValamisCollection from 'collections/valamis';
import ValamisModel from 'models/valamis';
import appConfig from 'config';
import App from 'application';
import * as enumerations from 'models/enumerations';
import StatementUtil from 'utils/statement-utils';

const GoalServices = new Backbone.Service({
    url(model) {
        return (model.isGroup()) ? appConfig().endpoint.goalGroups
            : appConfig().endpoint.goals;
    },
    targets: {
        move: {
            'path': 'move',
            'data': (model, options) => {
                let data = { indexNumber: options.index };
                if (options.parentId) {
                    _.extend(data, { groupId: options.parentId });
                }
                return data
            },
            'method': 'post'
        }
    }
});

const Goal = ValamisModel.extend({
    defaults: {
        active: false
    },
    isGroup() {
        return this.get('goalType') == enumerations.goalTypes.group;
    },
    isCompletedByCount() {
        return this.isGroup() && !isNaN(this.get('count'));
    },
    isActivity() {
        return this.get('goalType') == enumerations.goalTypes.activity;
    },
    isEvent() {
        return this.get('goalType') == enumerations.goalTypes.trainingEvent;
    },
    isLesson() {
        return this.get('goalType') == enumerations.goalTypes.lesson;
    },
    isWebContent() {
        return this.get('goalType') == enumerations.goalTypes.webContent;
    },
    isTincanLesson() { //todo add real checker
        return true;
    }
}).extend(GoalServices);

const GoalsServices = new Backbone.Service({
    sync: {
        'read': {
            'path': (collection, options) => {
                let path;
                if (options.getVersion) {
                    path = appConfig().endpoint.versions + collection.versionId + '/goals/tree';
                }
                else {
                    path = appConfig().endpoint.learningPaths + collection.pathId + '/goals/tree';
                }
                return path;
            },
            'method': 'get'
        }
    }
});

const Goals = ValamisCollection.extend({
    model: Goal,
    initialize() {
        this.url = '';
        ValamisCollection.prototype.initialize.apply(this, arguments);
    },
    parse(response, options) {
        let activities = {};
        _.each(options.activities, (item) => {
            activities[item.activityName] = item.title;
        });

        let setGoalData = (goals) => {
            _.each(goals, (item) => {
                if (item.goalType == enumerations.goalTypes.activity) {
                    item.title = activities[item.activityName];
                }
                if (item.goalType == enumerations.goalTypes.statement) {
                    let verb = StatementUtil.getVerbPostfix(item.verbId);
                    let langObject;
                    try {
                        langObject = JSON.parse(item.objectName);
                    } catch (e) {
                        langObject = item.objectName;
                    }

                    let obj = (typeof langObject === 'object')
                        ? StatementUtil.getLangDictionaryTincanValue(langObject)
                        : langObject;

                    item.title = (App.language[verb + 'VerbLabel'] || verb) + ' ' + obj;
                }

                if (item.goals) {
                    setGoalData(item.goals);
                }
            });
        };

        setGoalData(response);

        return response;
    }
}).extend(GoalsServices);

export default Goals
