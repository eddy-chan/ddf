/*global define, console*/
define(function (require) {
    "use strict";

// The module to be exported
    var ich = require('icanhaz'),
        _ = require('underscore'),
        moment = require('moment'),
        helper, helpers = {
            /*
             * Handlebars Helper: Moment.js
             * @author: https://github.com/Arkkimaagi
             * Built for Assemble: the static site generator and
             * component builder for Node.js, Grunt.js and Yeoman.
             * http://assemble.io
             *
             * Copyright (c) 2013, Upstage
             * Licensed under the MIT license.
             */
            moment: function (context, block) {
                var date, i;
                if (context && context.hash) {
                    block = _.cloneDeep(context);
                    context = undefined;
                }
                date = moment(context);

                // Reset the language back to default before doing anything else
                date.lang('en');

                for (i in block.hash) {
                    if (date[i]) {
                        date = date[i](block.hash[i]);
                    } else {
                        console.log('moment.js does not support "' + i + '"');
                    }
                }
                return date;
            },
            isNotBlank: function (context, block) {
                if (context && context !== "") {
                    return block.fn(this);
                }
                else {
                    return block.inverse(this);
                }
            },
            is: function (value, test, options) {
                if (value === test) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            },
            isnt: function (value, test, options) {
                if (value !== test) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }
            }
        };

// Export helpers
    for (helper in helpers) {
        if (helpers.hasOwnProperty(helper)) {
            ich.addHelper(helper, helpers[helper]);
        }
    }

});