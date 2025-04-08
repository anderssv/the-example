# Workshop System Context Diagram

```mermaid

graph TD
    AS[ApplicationService]
    AR[ApplicationRepository]
    CR[CustomerRegisterClient]
    UN[UserNotificationClient]
    CL[Clock]

    AS -->|uses| AR
    AS -->|requires active| CR
    AS -->|notifies| UN
    AS -->|uses| CL

```

The diagram above shows the main parts of the system:
- **ApplicationService**: Core service managing application logic
- **Repositories**: Handle data persistence for Applications and Customers
- **UserNotificationClient**: Manages user notifications
- **Clock**: Provides time-related functionality

# Domain Model (Application's Perspective)

```mermaid
graph TD
    A[Application]
    C[Customer]

    subgraph Application States
    ACTIVE[ACTIVE]
    APPROVED[APPROVED]
    DENIED[DENIED]
    EXPIRED[EXPIRED]
    end

    A -->|requires active| C
    A -->|can be| ACTIVE
    ACTIVE -->|can transition to| APPROVED
    ACTIVE -->|can transition to| DENIED
    ACTIVE -->|after 6 months| EXPIRED
```

The domain diagram above shows:
- **Application**: Core domain entity with its possible states
- **Customer**: Required for application processing
- **State Transitions**: Shows how an application can move between different states
- **Rules**:
    - Applications require an active customer
    - Active applications can be approved or denied
    - Applications expire after 6 months
