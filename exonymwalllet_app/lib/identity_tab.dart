import 'package:exonym/identity_row_item.dart';
import 'package:exonymwallet/exonymwallet.dart';
import 'package:flutter/cupertino.dart';
import 'package:provider/provider.dart';

import 'model/app_state_model.dart';

class IdentityTab extends StatelessWidget {
  const IdentityTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateModel>(
      builder: (context, model, child) {

        final List<IdentityContainer> identities = model.allIds();

        return CustomScrollView(
          semanticChildCount: identities.length,
          slivers: <Widget>[
            const CupertinoSliverNavigationBar(
              largeTitle: Text('Identities'),
            ),
            SliverSafeArea(               // ADD from here...
              top: false,
              minimum: const EdgeInsets.only(top: 0),
              sliver: SliverToBoxAdapter(
                child: CupertinoListSection(
                  topMargin: 0,
                  children:_buildIdentityList(identities),
                ),
              ),
            ),                            // ...to here.
          ],
        );
      },
    );
  }

  List<IdentityRowItem> _buildIdentityList(List<IdentityContainer> identities) {
    debugPrint("$identities");
    return <IdentityRowItem>[const IdentityRowItem(
        identity: IdentityContainer(
          identityType: IdentityType.all, username: 'Hello', b64Salt: ''))];

  }
}

