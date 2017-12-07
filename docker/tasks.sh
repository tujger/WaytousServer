#!/usr/bin/env bash
EMPTY=true
for i in "$@"
do
case $i in
    -b|--build)
    EMPTY=false
    BUILD=true
    ;;
    -r|--run)
    EMPTY=false
    RUN=true
    ;;
    -d|--run-detached)
    EMPTY=false
    DETACHED=true
    ;;
    -c|--console)
    EMPTY=false
    CONSOLE=true
    ;;
    -s|--stop)
    EMPTY=false
    STOP=true
    ;;
    -rc|--remove-containers)
    EMPTY=false
    REMOVE=true
    ;;
    -l|--list)
    EMPTY=false
    LIST=true
    ;;
    -ri|--remove-images)
    EMPTY=false
    REMOVEIMAGES=true
    ;;
    -v=*|--version=*)
    EMPTY=false
    VERSION="${i#*=}"
    ;;
    -p|--push)
    EMPTY=false
    PUSH=true
    ;;
    *)
            # unknown option
    ;;
esac
done

if [ $EMPTY == true ]; then
    echo tasks.sh -v=VERSION -s -rc -ri -b -r -d -c -l
    echo All keys are optional, see details
    echo    -b, --build - build/rebuild image
    echo    -r, --run - run container
    echo    -d, --run-detached - run container detached
    echo    -s, --stop - stop container
    echo    -l, --list - docker images
    echo    -rc, --remove-containers - remove all containers
    echo    -ri, --remove-images - remove all images forced
    echo    -c, --console - console in container
    exit 1
fi
if [ $STOP ]; then
    echo --- Stop container
    docker stop $(docker ps -q)
fi
if [ $REMOVE ]; then
    echo --- Remove containers
    docker rm $(docker images -q)
fi
if [ $REMOVEIMAGES ]; then
    echo --- Remove images forced
    docker rmi -f $(docker images -q)
fi
if [ $BUILD ]; then
    if [ $VERSION ]; then
        echo --- Set version: $VERSION
    else
        echo --- Define version: -v=VERSION
        exit 1;
    fi
    echo --- Build image
    docker build -t edeqa/waytous-server:$VERSION .. -f ./Dockerfile.$VERSION
fi
if [ $PUSH ]; then
    if [ $VERSION ]; then
        echo --- Set version: $VERSION
    else
        echo --- Define version: -v=VERSION
        exit 1;
    fi
    echo --- Publish image in repository
    docker push edeqa/waytous-server:$VERSION
fi
if [ $RUN ]; then
    if [ $VERSION ]; then
        echo --- Set version: $VERSION
    else
        echo --- Define version: -v=VERSION
        exit 1;
    fi
    echo --- Run container
    docker run -p 8080:8080 -p 8100:8100 -p 8101:8101 -p 8200:8200 -p 8201:8201 -p 8443:8443 -p 8989:8989 edeqa/waytous-server:$VERSION
#    docker run -p 8080:8080 -p 8100:8100 -p 8101:8101 -p 8200:8200 -p 8201:8201 -p 8443:8443 -p 8989:8989 edeqa/waytous-server:1.50
fi
if [ $DETACHED ]; then
    if [ $VERSION ]; then
        echo --- Set version: $VERSION
    else
        echo --- Define version: -v=VERSION
        exit 1;
    fi
    echo --- Run container detached, see waytous.log
    docker run -d -p 8080:8080 -p 8100:8100 -p 8101:8101 -p 8200:8200 -p 8201:8201 -p 8443:8443 -p 8989:8989 edeqa/waytous-server:$VERSION > waytous.log
#    docker run -p 8080:8080 -p 8100:8100 -p 8101:8101 -p 8200:8200 -p 8201:8201 -p 8443:8443 -p 8989:8989 edeqa/waytous-server:1.50
fi
if [ $CONSOLE ]; then
    if [ $VERSION ]; then
        echo --- Set version: $VERSION
    else
        echo --- Define version: -v=VERSION
        exit 1;
    fi
    echo --- Console in container
    docker run -i -t edeqa/waytous-server:$VERSION /bin/bash
fi
if [ $LIST ]; then
    echo --- List images
    docker images
fi


