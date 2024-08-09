package com.github.learntocode2013;

import com.github.learntocode2013.CustomStructuredTaskScope.PublicTransportOffer;
import com.github.learntocode2013.CustomStructuredTaskScope.RideSharingOffer;

public sealed interface Travel permits RideSharingOffer, PublicTransportOffer {}
