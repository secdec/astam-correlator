package com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators;

import com.denimgroup.threadfix.framework.impl.dotNet.DotNetControllerMappings;

import java.util.List;

public interface AspActionGenerator {
    List<DotNetControllerMappings> generate();
}
