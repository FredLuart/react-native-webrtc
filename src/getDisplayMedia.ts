
import { NativeModules } from 'react-native';

import { MediaTrackConstraints } from './Constraints';
import MediaStream from './MediaStream';
import MediaStreamError from './MediaStreamError';
import * as RTCUtil from './RTCUtil';

const { WebRTCModule } = NativeModules;

export interface Constraints {
    audio?: boolean | MediaTrackConstraints;
    video?: boolean | MediaTrackConstraints;
}

export default function getDisplayMedia(constraints: Constraints = {}): Promise<MediaStream> {

    // Normalize constraints.
    constraints = RTCUtil.normalizeConstraints(constraints);

    return new Promise((resolve, reject) => {
        WebRTCModule.getDisplayMedia(constraints).then(
            data => {
                const { streamId, track } = data;

                const info = {
                    streamId: streamId,
                    streamReactTag: streamId,
                    tracks: [ track ]
                };

                const stream = new MediaStream(info);

                resolve(stream);
            },
            error => {
                reject(new MediaStreamError(error));
            }
        );
    });
}
