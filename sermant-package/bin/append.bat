@echo off

@rem Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem     http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.

@rem author: HapThorin
@rem date: 2021/12/08

set "source=%1%"&& shift
set "target=%1%"&& shift
set "source=%source:/=\%"
set "target=%target:/=\%"
if exist %source% (
    type %source% >> %target%
)
