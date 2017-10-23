/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/10/17.
 */
EVENTS.SAMPLE_EVENT = "sample_event";

function SampleHolder(main) {

    var type = "sample";

    function start() {
        console.log("SAMPLEHOLDER", this);
    }

    function onEvent(EVENT,object){
        console.warn("EVENT",this && this.number || "Main", EVENT, object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                var menuItem = object.add(DRAWER.SECTION_PRIMARY, EVENTS.SAMPLE_EVENT, u.lang.sample_item, "ac_unit", function(){
                    console.log("SAMPLEEVENTDRAWERCALLBACK", EVENT);
                });
                menuItem.classList.add("disabled");
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user) {
                    object.add(MENU.SECTION_PRIMARY, EVENT.SAMPLE_EVENT, u.lang.sample_menu, "ac_unit", function () {
                        u.save("sample:show:"+user.number, true);
                        console.log("SAMPLEEVENTMENUCALLBACK", user);
                    });
                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        var view = {
            user: user,
            show: u.load("sample:user:" + user.number)
        };
        // console.log("SAMPLECREATEVIEW",user);
        return view;
    }

    function onChangeLocation(location) {
        // console.log("SAMPLEONCHANGELOCATION",this,location);
    }

    function help(){
        return {
            title: u.lang.sample_title,
            1: {
                title: u.lang.sample_title_1,
                body: u.lang.sample_body_1
            },
            2: {
                title: u.lang.sample_title_2,
                body: u.lang.sample_body_2,
            },
            3: {
                title: u.lang.sample_title_3,
                body: u.lang.sample_body_3,
                ignore: true,
            }
        }
    }

    var resources = {
        sample_item: "Sample item",
        sample_menu: "Sample menu",
        sample_title: "Sample",
        sample_title_1: "Article 1",
        sample_body_1: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        sample_title_2: "Article 2",
        sample_body_2: "Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.",
        sample_title_3: "Additional article",
        sample_body_3: "Additional body.",
        sample_option: "Sample option"
    };

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:sample",
                    title: "Sample",
                    items: [
                        {
                            id: "general:sample",
                            type: HTML.CHECKBOX,
                            label: "Sample option",
                            checked: u.load("general:sample"),
                            onaccept: function(e, event) {
                                u.save("general:sample", this.checked);
                            },
                            onchange: function(e, event) {
                                u.toast.show("general:sample " + this.checked);
                            },
                            onshow: function(e) {
                                u.toast.show("general:sample " + this.checked);
                            },
                        }
                    ]
                }
            ]
        }
    }


    return {
        createView:createView,
        help:help,
        onChangeLocation:onChangeLocation,
        onEvent:onEvent,
        options:options,
        resources:resources,
        start:start,
        type:type,
    }
}