### Firebase rules for Waytous

    {
      "rules": {
        ".read": "auth.uid === 'Administrator' || auth.uid === 'Viewer'",
        ".write": "auth.uid === 'Administrator'",
        "_g":{ // groups
          ".write": false,
          "$group_id": {
            ".write": false,
            ".validate": "root.child('_g').child($group_id).exists()",
            //".write": "(auth.uid != null && !data.exists() && newData.exists()) || auth.uid === 'rdvGxl3Yc0boERLyh71Ur8IaMen1'",
            "o": { // options
              ".read": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
              ".write": false,
              "dateCreated": {
                ".write": "!data.exists() && newData.exists()"
              },
              "delayToDismiss": {
                ".write": "!data.exists() && newData.exists()"
              },
              "dismissInactive": {
                ".write": "!data.exists() && newData.exists()"
              },
              "limitUsers": {
                ".write": "!data.exists() && newData.exists()"
              },
              "persistent": {
                ".write": "!data.exists() && newData.exists()"
              },
              "requiresPassword": {
                ".write": "!data.exists() && newData.exists()"
              },
              "timeToLiveIfEmpty": {
                ".write": "!data.exists() && newData.exists()"
              },
              "welcomeMessage": {
                ".write": "root.child('_g/'+$group_id+'/u/p/0/uid').val() === auth.uid",
                ".validate": "newData.isString() && newData.val().length < 10240"
              }
            },
            "u": { // users
              ".read": false,
              ".write": false,
              "n": { // uniqueKey - uid
                ".read": false,
                ".write": false
              },
              "k": { // uid - number
                ".read": false,
                ".write": false
              },
              "b": { // data
                ".read": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
                "$user_number":{
                  "active": {
                    ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                    ".validate": "newData.isBoolean()"
                  },
                  "name": {
                    ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                    ".validate": "newData.isString() && newData.val().length < 256"
                  },
                  "ch": {
                    ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                    ".validate": "newData.val() <= now"
                  }
                }
              },
              "p": { // data-private
                ".read": false,
                ".write": false
              }
            },
            "b": { // public
              ".read": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
              ".write": false,
              "$event": {
                "$user_number": {
                  ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                  "$item_id": {
                    ".validate": "(newData.isString() && newData.val().length < 10240) || !newData.isString()",
                    "$key_id": {
                      ".validate": "(newData.isString() && newData.val().length < 10240) || newData.isNumber() || newData.isBoolean()"
                    }
                  }
                }
              }
            },
            "p": { // private
              ".read": false,
              ".write": false,
              "$event": {
                "$user_number": {
                  ".read": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                  "$item_id": {
                    ".write": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
                    ".validate": "(newData.isString() && newData.val().length < 10240) || !newData.isString()",
                    "$key_id": {
                      ".validate": "(newData.isString() && newData.val().length < 10240) || newData.isNumber() || newData.isBoolean()"
                    }
                  }
                }
              }
            }
            //    ".read": "root.child($group_id+'/u/'+auth.uid).exists()",
            //    ".write": "root.child($group_id+'/u/'+auth.uid).exists() && newData.child('author_id').val() === auth.uid && !data.exists() && newData.exists()",
          }
        },
        "_s": { // stat
          ".write": false
        },
        "_u": { // users
          ".write": false,
          "$user_id": {
            ".read": "$user_id === auth.uid",
            ".write": "$user_id === auth.uid && root.child('_u').child($user_id).exists()",
            "p": { // user persistent private data
              "cr": {
                ".validate": "!data.exists() && newData.exists()"
              },
              "signProvider": {
                ".validate": "!data.exists() && newData.exists()"
              },
              "model": {
                ".validate": "!data.exists() && newData.exists()"
              },
              "manufacturer": {
                ".validate": "!data.exists() && newData.exists()"
              },
              "os": {
                ".validate": "!data.exists() && newData.exists()"
              }
            }
          }
        }
      }
    }
    
    
Old:
    
    {
      "rules": {
        ".read": "auth.uid === 'Administrator' || auth.uid === 'Viewer'",
        ".write": "auth.uid === 'Administrator'",
        "_g":{ // groups
          ".write": false,
          "$group_id": {
            ".write": false,
            ".validate": "root.child('_g').child($group_id).exists()",
            //".write": "(auth.uid != null && !data.exists() && newData.exists()) || auth.uid === 'rdvGxl3Yc0boERLyh71Ur8IaMen1'",
            "o": { // options
              ".read": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
              ".write": false,
              "welcome-message": {
                ".write": "root.child('_g/'+$group_id+'/u/p/0/uid').val() === auth.uid",
                ".validate": "newData.isString() && newData.val().length < 10240"
              },
              "date-created": {
                ".write": "!data.exists() && newData.exists()"
              },
              "persistent": {
                ".write": "!data.exists() && newData.exists()"
              },
              "requires-password": {
                ".write": "!data.exists() && newData.exists()"
              },
              "time-to-live-if-empty": {
                ".write": "!data.exists() && newData.exists()"
              },
              "dismiss-inactive": {
                ".write": "!data.exists() && newData.exists()"
              },
              "delay-to-dismiss": {
                ".write": "!data.exists() && newData.exists()"
              }
            },
            "u": { // users
              ".read": false,
              ".write": false,
              "n": { // uniqueKey - uid
                ".read": false,
                ".write": false
              },
              "k": { // uid - number
                ".read": false,
                ".write": false
              },
              "b": { // data
                ".read": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
                "$user_number":{
                  "active": {
                    ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                    ".validate": "newData.isBoolean()"
                  },
                  "name": {
                    ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                    ".validate": "newData.isString() && newData.val().length < 256"
                  },
                  "ch": {
                    ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                    ".validate": "newData.val() <= now"
                  }
                }
              },
              "p": { // data-private
                ".read": false,
                ".write": false
              }
            },
            "b": { // public
              ".read": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
              ".write": false,
              "$event": {
                "$user_number": {
                  ".write": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                  "$item_id": {
                    ".validate": "(newData.isString() && newData.val().length < 10240) || !newData.isString()",
                    "$key_id": {
                      ".validate": "(newData.isString() && newData.val().length < 10240) || newData.isNumber() || newData.isBoolean()"
                    }
                  }
                }
              }
            },
            "p": { // private
              ".read": false,
              ".write": false,
              "$event": {
                "$user_number": {
                  ".read": "root.child('_g/'+$group_id+'/u/p/'+$user_number+'/uid').val() === auth.uid",
                  "$item_id": {
                    ".write": "root.child('_g/'+$group_id+'/u/k/'+auth.uid).exists()",
                    ".validate": "(newData.isString() && newData.val().length < 10240) || !newData.isString()",
                    "$key_id": {
                      ".validate": "(newData.isString() && newData.val().length < 10240) || newData.isNumber() || newData.isBoolean()"
                    }
                  }
                }
              }
            }
      //    ".read": "root.child($group_id+'/u/'+auth.uid).exists()",
      //    ".write": "root.child($group_id+'/u/'+auth.uid).exists() && newData.child('author_id').val() === auth.uid && !data.exists() && newData.exists()",
    
          }
        },
        "_s": { // stat
          ".write": false
        },
        "_u": { // users
          ".write": false,
          "$user_id": {
                ".read": "$user_id === auth.uid",
            ".write": "$user_id === auth.uid && root.child('_u').child($user_id).exists()",
            "p": { // user persistent private data
              "cr": {
                  ".validate": "!data.exists() && newData.exists()"
                },
              "sign-provider": {
                ".validate": "!data.exists() && newData.exists()"
                },
              "model": {
                  ".validate": "!data.exists() && newData.exists()"
                },
              "manufacturer": {
                ".validate": "!data.exists() && newData.exists()"
                },
              "os": {
                  ".validate": "!data.exists() && newData.exists()"
                }
            }
          }
        }
      }
    }