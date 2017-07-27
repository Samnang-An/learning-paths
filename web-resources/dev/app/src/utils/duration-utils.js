
import LiferayUtil from 'utils/liferay-utils';
import moment from 'moment';
import humanizeDuration from "humanize-duration";
import * as enumerations from 'models/enumerations';

// isoRegex, parseIso and parseDuration have been copied from moment-with-locales.js 2.10.0
// because they didn't provide following api's
const isoRegex = /^(-)?P(?:(-?[0-9,.]*)Y)?(?:(-?[0-9,.]*)M)?(?:(-?[0-9,.]*)W)?(?:(-?[0-9,.]*)D)?(?:T(?:(-?[0-9,.]*)H)?(?:(-?[0-9,.]*)M)?(?:(-?[0-9,.]*)S)?)?$/;

const parseIso = (inp, sign) => {
    // We'd normally use ~~inp for this, but unfortunately it also
    // converts floats to ints.
    // inp may be undefined, so careful calling replace on it.
    let res = inp && parseFloat(inp.replace(',', '.'));
    // apply sign while we're at it
    return (isNaN(res) ? 0 : res) * sign;
};

const parseDuration = (input) => {
    let sign, duration, match = null;
    if (!!(match = isoRegex.exec(input))) {
        sign = (match[1] === '-') ? -1 : 1;
        duration = {
            y: parseIso(match[2], sign),
            mo: parseIso(match[3], sign),
            w: parseIso(match[4], sign),
            d: parseIso(match[5], sign),
            h: parseIso(match[6], sign),
            m: parseIso(match[7], sign),
            s: parseIso(match[8], sign)
        };
    }

    return duration
};

class DurationUtil {
    static getDurationObject(duration) {
        let result = {};
        _.each(parseDuration(duration), (v, k) => { if(v > 0) {result[k] = v} });
        return result;
    }

    static getHumanizeDuration(duration) {
        let ms = moment.duration(duration).asMilliseconds();
        let units = _.map(this.getDurationObject(duration), (v, k) => k);
        return humanizeDuration(ms, {language: LiferayUtil.getUserLocale(), units: units, round: true });
    }

    static getIsoDuration(obj) {
        let iso = 'P';
        if (obj.y) { iso += obj.y + 'Y'}
        if (obj.mo) { iso += obj.mo + 'M'}
        if (obj.w) { iso += obj.w + 'W'}
        if (obj.d) { iso += obj.d + 'D'}
        if (obj.h || obj.m || obj.s ) {
            iso += 'T';
            if (obj.h) { iso += obj.h + 'H'}
            if (obj.m) { iso += obj.m + 'M'}
            if (obj.s) { iso += obj.s + 'S'}
        }

        return iso;
    }

    static getPeriod(duration) {
        // take from duration object only periods existing in period selector using intersection
        // if there is no suitable period, show warning
        // if there are several - take the first one
        let durationObject = this.getDurationObject(duration);

        let periodTypes = enumerations.validPeriodTypes;
        let allowedTL = _.intersection(_.keys(durationObject), _.keys(periodTypes));

        let period = {};
        if (allowedTL.length > 0) {
            period.type = allowedTL[0];
            period.value = durationObject[allowedTL[0]];
        }

        return period;
    }
}

export default DurationUtil;
