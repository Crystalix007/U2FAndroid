#pragma once

#include "../U2FDevice/U2FMessage.hpp"

std::unique_ptr<U2FMessage>& getPending();
