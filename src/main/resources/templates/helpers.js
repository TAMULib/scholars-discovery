///////////////////////////////////////////////////////////////////////////////
// Handlebar helpers used in server side templating of persisted templates.
// 
// Available by TemplateService for export.
///////////////////////////////////////////////////////////////////////////////

/**
 * Get current year.
 * 
 * returns current year
 */
Handlebars.registerHelper('year', function () {
    return Number(new Date().getUTCFullYear()).toString();
});

/**
 * Get current year minus input.
 * 
 * returns current year minus input
 */
Handlebars.registerHelper('yearFrom', function (input) {
    return Number(new Date().getUTCFullYear() - input).toString();
});

/**
 * Get year for string input. See JavaScript Date.
 * 
 * returns year as number
 */
Handlebars.registerHelper('toYear', function (value) {
    if (value) {
        const date = new Date(value);
        value = Number(date.getUTCFullYear()).toString();
    }
    return value;
});

/**
 * Get unique set of types from a collection in which can have multiple types.
 *
 * Used to recopitulate layout already provided by two other clients:
 *   scholars-angular
 *   scholars-embed
 *
 * This presents issues in presentation while sorting into categories without duplication.
 * How to represent an entry under its first category that also belongs to another?
 * 
 * ▒▒▒▒▒▒▒▒▄▄▄▄▄▄▄▄▒▒▒▒▒▒▒▒
 * ▒▒▒▒▒▄█▀▀░░░░░░▀▀█▄▒▒▒▒▒
 * ▒▒▒▄█▀▄██▄░░░░░░░░▀█▄▒▒▒
 * ▒▒█▀░▀░░▄▀░░░░▄▀▀▀▀░▀█▒▒
 * ▒█▀░░░░███░░░░▄█▄░░░░▀█▒
 * ▒█░░░░░░▀░░░░░▀█▀░░░░░█▒
 * ▒█░░░░░░░░░░░░░░░░░░░░█▒
 * ▒█░░██▄░░▀▀▀▀▄▄░░░░░░░█▒
 * ▒▀█░█░█░░░▄▄▄▄▄░░░░░░█▀▒
 * ▒▒▀█▀░▀▀▀▀░▄▄▄▀░░░░▄█▀▒▒
 * ▒▒▒█░░░░░░▀█░░░░░▄█▀▒▒▒▒
 * ▒▒▒█▄░░░░░▀█▄▄▄█▀▀▒▒▒▒▒▒
 * ▒▒▒▒▀▀▀▀▀▀▀▒▒▒▒▒▒▒▒▒▒▒▒▒
 *
 * returns set of types within resource collection
 */
Handlebars.registerHelper('eachTypesFor', function (resources, options) {
    let out = '';
    if (resources) {
        resources = JSON.parse(resources);
        const u = [];
        for (const i in resources) {
            if (resources.hasOwnProperty(i)) {
                const resource = resources[i];
                for (const j in resource.type) {
                    if (resource.type.hasOwnProperty(j)) {
                        const type = resource.type[j];
                        if (u.indexOf(type) < 0) {
                            u.push(type);
                            out += options.fn(type);
                        }
                    }
                }
            }
        }
    }
    return out;
});

/**
 * Filters resources by type provided.
 * 
 * The type property of each resource is expected to be an array as multiple in index document.
 * 
 * resource: map or respectively JSON object with at least type top level property and it being an array of strings
 * type: value filtering by
 * options: see Handlebars.js
 * 
 * return resources by type
 */
Handlebars.registerHelper('eachFilteredByType', function (resources, type, options) {
    let out = '';
    if (resources) {
        resources = JSON.parse(resources).filter(function (r) {
            return r.type.indexOf(type) >= 0;
        });
        for (const i in resources) {
            if (resources.hasOwnProperty(i)) {
                out += options.fn(resources[i]);
            }
        }
    }
    return out;
});

/**
 * Quick fix to formalize a label with best attempt to represent its purpose semantically.
 * 
 * value: string
 * return see switch below else value
 */
Handlebars.registerHelper('toFormalRole', function (value) {
    if (value) {
        switch (value) {
            case "PrincipalInvestigatorRole": return "Principal Investigator (PI)";
            case "CoPrincipalInvestigatorRole": return "Co-Principal Investigator (Co-PI)";
            case "InvestigatorRole": return "Investigator";
        }
    }
    return value;
});

/**
 * Quick fix to formalize a label with best attempt to represent its purpose semantically.
 * 
 * duplication of `toFormalRole` above with different signature and transform
 * 
 * value: string
 * return see switch below else value
 */
Handlebars.registerHelper('toSubsectionTypeLabel', function (value) {
    if (value) {
        switch (value) {
            case "AcademicArticle": return "Academic Articles";
            case "EditorialArticle": return "Editorial Articles";
            case "Book": return "Books";
            case "Chapter": return "Chapters";
            case "ConferencePaper": return "Conference Papers";
            case "GreyLiterature": return "Repository Documents / Preprints";
            case "Thesis": return "Theses";
            case "WorkingPaper": return "Working Papers";
            case "Webpage": return "Internet Publications";
            case "Report": return "Reports";
            case "Review": return "Reviews";
            case "Review": return "Reviews";
            case "Patent": return "Patents";
        }
    }
    return value;
});

/**
 * Helper method to handle serialized array into JavaScript intended use parsing author list
 * from triplestore object string literal.
 * 
 * value: a string
 * 
 * returns value removing array characters from start and end
 */
Handlebars.registerHelper('parseAuthorList', function (value) {
    if (value) {
        value = value.toString();
        if (value.startsWith('["')) {
            value = value.substring(2);
        }
        if (value.endsWith('"]')) {
            value = value.substring(0, value.length - 2);
        }
        return value;
    }
    return value;
});

/**
 * Split input string by delimeter returning each trimmed as an array.
 * 
 * value: delimeted string
 * delimeter: character or string to delimite by
 * options: see Handlebars.js
 * 
 * returns array of strings
 */
Handlebars.registerHelper('eachSplit', function (value, delimeter, options) {
    let out = '';
    if (value) {
        value = value.toString();
        const parts = value.split(delimeter);
        for (const i in parts) {
            if (parts.hasOwnProperty(i)) {
                out += options.fn(parts[i].trim());
            }
        }
    }
    return out;
});

/**
 * Iterate after sorting.
 * 
 * resources: set to sort
 * field: field to sort by
 * direction: sort direction `asc` or `desc`
 * isDate: whether the value for the field is of type date
 * options: see Handlebars.js
 * 
 * returns sorted list of resources
 */
Handlebars.registerHelper('eachSorted', function (resources, field, direction, isDate, options) {
    let out = '';
    if (resources) {
        direction = (direction && direction.toLowerCase() === 'asc') ? [1, -1] : [-1, 1];
        resources = JSON.parse(resources).sort(function (r1, r2) {
            const v1 = isDate ? new Date(r1[field]) : r1[field];
            const v2 = isDate ? new Date(r2[field]) : r2[field];
            if (v1 > v2) {
                return direction[0];
            }
            if (v1 < v2) {
                return direction[1];
            }
            return 0;
        });
        for (const i in resources) {
            if (resources.hasOwnProperty(i)) {
                out += options.fn(resources[i]);
            }
        }
    }
    return out;
});

// in JavaScript, must be declared before interpreted, otherwise undefined
// see below for its use
function filterCurrentFunding(resources) {
    if (!resources) {
        return [];
    }
    resources = JSON.parse(resources).sort(function (r1, r2) {
        const v1 = r1['label'];
        const v2 = r2['label'];
        if (v1 > v2) {
            return 1;
        }
        if (v1 < v2) {
            return -1;
        }
        return 0;
    });
    const now = new Date();
    return resources.sort(function (r1, r2) {
        const v1 = new Date(r1['startDate']);
        const v2 = new Date(r2['startDate']);
        if (v1 > v2) {
            return -1;
        }
        if (v1 < v2) {
            return 1;
        }
        return 0;
    }).filter(function (r) {
        const startDate = new Date(r['startDate']);
        const endDate = new Date(r['endDate']);
        return startDate <= now && endDate >= now;
    });
}

/**
 * Filter input resources by those that have a date range.
 * 
 * Currently only used with document field `researcherOn`.
 * 
 * `researcherOn` is derived from person by obo:RO_0000053 filtered by vivo:ResearcherRole vivo:relatedBy as a grant.
 * 
 * resources: array of grants
 * 
 * returns grants with current funding as conditioned by having a open end date range
 */
Handlebars.registerHelper('eachCurrentFunding', function (resources, options) {
    resources = filterCurrentFunding(resources);
    let out = '';
    for (const i in resources) {
        if (resources.hasOwnProperty(i)) {
            out += options.fn(resources[i]);
        }
    }
    return out;
});

/**
 * Filter and check if any of input resources has date range.
 *
 * Currently only used with document field `researcherOn`.
 * 
 * resources: grants
 * 
 * returns whether the grants have current funding
 */
Handlebars.registerHelper('hasCurrentFunding', function (resources, options) {
    return filterCurrentFunding(resources).length > 0
        ? options.fn(this)
        : options.inverse(this);
});

